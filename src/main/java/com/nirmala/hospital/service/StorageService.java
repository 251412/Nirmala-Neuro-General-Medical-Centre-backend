package com.nirmala.hospital.service;

import com.nirmala.hospital.model.UploadedFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {
    UploadedFile store(MultipartFile file);
    List<UploadedFile> storeBatch(List<MultipartFile> files);
    boolean deleteByUrl(String imageUrl);
}
