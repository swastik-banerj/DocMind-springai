package com.springai.docmind.Controller;

import com.springai.docmind.Service.DocumentMetadataService;
import com.springai.docmind.dto.ApiResponse;
import com.springai.docmind.dto.DocumentResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(
        name = "Document Management",
        description = "Endpoints for uploading, listing and managing documents and their vector embeddings"
)
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentMetadataService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload and index document(PDF,CSV,DOCX,TEXT,MD)"
    )
    public ResponseEntity<ApiResponse<DocumentResponseDto>> uploadDocument(@RequestParam("file")MultipartFile file){

        // process the file
        DocumentResponseDto documentResponseDto = this.documentService.uploadAndProcess(file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DocumentResponseDto>builder()
                        .success(true)
                        .data(documentResponseDto)
                        .timeStamp(LocalDateTime.now())
                        .message("Documents uploaded and indexed successfully")
                        .build());

    }
}
