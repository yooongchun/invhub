package zoz.cool.apihub.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public interface StorageService {
    String upload(MultipartFile file);

    String upload(byte[] fileBytes, String fileName);

    byte[] download(String savePath);

    String preview(String savePath);
}
