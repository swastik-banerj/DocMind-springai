package com.springai.docmind.Service;

import com.springai.docmind.Entity.DocumentMetadata;
import com.springai.docmind.Entity.DocumentStatus;
import com.springai.docmind.Exception.DocumentProcessingException;
import com.springai.docmind.Repository.DocumentMetadataRepository;
import com.springai.docmind.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final VectorStore vectorStore;
    private final DocumentMetadataRepository documentMetadataRepository;
    private final AppProperties appProperties;

    public int ingest(DocumentMetadata documentMetadata, List<Document> parsedDocs) {
        log.info("Ingesting document [id={}, name={}, pages={}]", documentMetadata.getId(),documentMetadata.getFilename(), documentMetadata.getFileSize());

        try{

            documentMetadata.setStatus(DocumentStatus.PROCESSING);
            documentMetadata.setTotalPages(parsedDocs.size());

            //1. Text chunking using TokenTextSplitter
            TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder()
                    .withChunkSize(appProperties.getRag().getChunkSize())
                    .withMinChunkLengthToEmbed(appProperties.getRag().getMinChunkLengthToEmbed())
                    .withMinChunkSizeChars(appProperties.getRag().getMinChunkSizeChars())
                    .withMaxNumChunks(appProperties.getRag().getMaxNumChunks())
                    .withKeepSeparator(true)
                    .build();

            List<Document> chunks =  tokenTextSplitter.apply(parsedDocs);
            if(chunks.isEmpty()){
                documentMetadata.setStatus(DocumentStatus.FAILED);
                documentMetadata.setErrMessage("Document is empty or unscannable");
                documentMetadataRepository.save(documentMetadata);
                return 0;
            }

            //2. Meta data enrichment on each chunk
            List<Document> enrichedChunks = new ArrayList<>();
            for(int i=0; i < chunks.size(); i++){
                Document chunk = chunks.get(i);
                Map<String, Object> enrichedMetadata = new HashMap<>(chunk.getMetadata());
                enrichedMetadata.put("documentId", documentMetadata.getId().toString());
                enrichedMetadata.put("fileName", documentMetadata.getFilename());
                enrichedMetadata.put("contentType", documentMetadata.getContentType());
                enrichedMetadata.put("chunkIndex", i);

                // Preserve or calculte pageNumber if available
                Object pageNumber = chunk.getMetadata().get("page_number");
                if(pageNumber==null){
                    pageNumber = chunk.getMetadata().get("pageNumber");
                }

                if(pageNumber!=null){
                    enrichedMetadata.put("pageNumber", pageNumber);
                }

                Document enrichedDoc = new Document(chunk.getText(), enrichedMetadata);
                enrichedChunks.add(enrichedDoc);
            }

            //3. write chunks and embeddings to pgvector
            log.info("Writing {} vector chunks to PgVectorStore for document: {}", enrichedChunks.size(), documentMetadata.getFilename());
            vectorStore.add(enrichedChunks);

            //4. Update document status to index
            documentMetadata.setStatus(DocumentStatus.INDEXED);
            documentMetadata.setTotalChunks(enrichedChunks.size());
            documentMetadata.setErrMessage(null);
            documentMetadataRepository.save(documentMetadata);
            log.info("Successfully indexed document [id={}, name={}, chunks={}]", documentMetadata.getId(), documentMetadata.getFilename(), enrichedChunks.size());

            return enrichedChunks.size();
        } catch (Exception ex) {
            log.error("Failed to ingest document into vector store: {}", documentMetadata.getFilename(), ex);
            documentMetadata.setStatus(DocumentStatus.FAILED);
            documentMetadata.setErrMessage(ex.getMessage());
            documentMetadataRepository.save(documentMetadata);
            throw new DocumentProcessingException("Failed to index document: " + ex.getMessage(), ex);
        }
    }
}
