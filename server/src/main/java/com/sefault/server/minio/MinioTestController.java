package com.sefault.server.minio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Profile("dev")
@RestController
@RequestMapping("/test/api/minio")
@RequiredArgsConstructor
@Tag(name = "Test File Operations", description = "Endpoints for managing MinIO storage assets")
public class MinioTestController {
    private final MinioService minioService;

    @Operation(
            summary = "Upload a new file",
            description = "Uploads a multipart file to the MinIO bucket and assigns it the provided object name.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "File successfully uploaded"),
                @ApiResponse(responseCode = "500", description = "Internal server error during upload process")
            })
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @Parameter(description = "The physical file payload to upload", required = true) @RequestPart("file")
                    MultipartFile file,
            @Parameter(
                            description = "The exact target name for the file in the bucket (e.g., 'image.png')",
                            required = true)
                    @RequestParam("objectName")
                    String objectName) {
        try {
            minioService.uploadFile(objectName, file);
            return ResponseEntity.ok("File uploaded successfully as: " + objectName);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Gets a temporary url to a file",
            description = "Returns a url which allows temporary access to the file for a limited time")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Temporary URL successfully generated"),
                @ApiResponse(responseCode = "404", description = "File not found"),
                @ApiResponse(responseCode = "500", description = "Internal server error during URL generation")
            })
    @GetMapping("/url/temp")
    public ResponseEntity<String> getTemporaryUrl(
            @Parameter(description = "The exact name of the file in the bucket", required = true)
                    @RequestParam("objectName")
                    String objectName) {
        try {
            String url = minioService.getFileUrl(objectName, 60 * 60);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to generate URL: " + e.getMessage());
        }
    }

    @Operation(summary = "Gets a permanent url to a file", description = "Returns a permanent url to access the file")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Permanent URL successfully generated")})
    @GetMapping("/url/permanent")
    public ResponseEntity<String> getPermanentUrl(
            @Parameter(description = "The exact name of the file in the bucket", required = true)
                    @RequestParam("objectName")
                    String objectName) {
        String url = minioService.getPermanentFileUrl(objectName);
        return ResponseEntity.ok(url);
    }

    @Operation(summary = "Deletes a file", description = "Deletes the specified file from the MinIO bucket")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "File successfully deleted"),
                @ApiResponse(responseCode = "500", description = "Internal server error during deletion process")
            })
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(
            @Parameter(description = "The exact name of the file to delete from the bucket", required = true)
                    @RequestParam("objectName")
                    String objectName) {
        try {
            minioService.deleteFile(objectName);
            return ResponseEntity.ok("File deleted successfully: " + objectName);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Deletion failed: " + e.getMessage());
        }
    }
}
