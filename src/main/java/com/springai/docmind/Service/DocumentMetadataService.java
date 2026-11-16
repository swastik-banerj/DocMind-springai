package com.springai.docmind.Service;

import com.springai.docmind.Entity.DocumentMetadata;
import com.springai.docmind.Entity.DocumentStatus;
import com.springai.docmind.Repository.DocumentMetadataRepository;
import com.springai.docmind.dto.DocumentResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentMetadataService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentMetadataService.class);

    private final DocumentMetadataRepository documentMetadataRepository;
    private final ParserService parserService;
    private final IngestionService ingestionService;
    private final JdbcTemplate jdbcTemplate;


    // method to upload and parse document
    public DocumentResponseDto uploadAndProcess(MultipartFile file) {

        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        // document meta data create
        DocumentMetadata documentMetadata = DocumentMetadata.builder()
                .filename(fileName)
                .contentType(contentType)
                .status(DocumentStatus.UPLOADING)
                .fileSize(file.getSize())
                .build();

        // save the document meta data
        documentMetadata = documentMetadataRepository.save(documentMetadata);

        // parse the file
        List<Document> parsedDocs = parserService.parse(file);

        // ingest service
        int chunksCreated = ingestionService.ingest(documentMetadata, parsedDocs);

        //documentMetadata.setTotalChunks(chunksCreated);


        return DocumentResponseDto.builder()
                .id(documentMetadata.getId())
                .fileName(documentMetadata.getFilename())
                .fileSize(documentMetadata.getFileSize())
                .chunksCreated(chunksCreated)
                .status(documentMetadata.getStatus())
                .message("Document successfully processed and indexed")
                .build();
    }
}
