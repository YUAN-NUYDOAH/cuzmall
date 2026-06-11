package cn.zjicm.transaction.upload;

import cn.zjicm.transaction.config.UploadProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
public class ProductImageStorage {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final UploadProperties uploadProperties;

    public ProductImageStorage(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getSize() > uploadProperties.getMaxSizeBytes()) {
            throw new ImageUploadException("商品图片不能超过 5MB。");
        }

        String extension = resolveExtension(file);
        if (extension == null) {
            throw new ImageUploadException("仅支持 JPG、PNG、WEBP 格式的图片。");
        }

        try {
            Path productDir = Paths.get(uploadProperties.getDirectory(), "products").toAbsolutePath().normalize();
            Files.createDirectories(productDir);
            String filename = UUID.randomUUID() + "." + extension;
            Path target = productDir.resolve(filename).normalize();
            if (!target.startsWith(productDir)) {
                throw new ImageUploadException("图片保存路径无效。");
            }
            file.transferTo(target);
            return "/uploads/products/" + filename;
        } catch (IOException exception) {
            throw new ImageUploadException("图片保存失败，请稍后再试。");
        }
    }

    private String resolveExtension(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            String extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
            if (ALLOWED_EXTENSIONS.contains(extension)) {
                return extension.equals("jpeg") ? "jpg" : extension;
            }
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return null;
        }
        switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg":
                return "jpg";
            case "image/png":
                return "png";
            case "image/webp":
                return "webp";
            default:
                return null;
        }
    }
}
