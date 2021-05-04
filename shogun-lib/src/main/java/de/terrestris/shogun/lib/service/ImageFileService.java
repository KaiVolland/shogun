/* SHOGun, https://terrestris.github.io/shogun/
 *
 * Copyright © 2020-present terrestris GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.terrestris.shogun.lib.service;

import de.terrestris.shogun.lib.model.ImageFile;
import de.terrestris.shogun.lib.repository.ImageFileRepository;
import de.terrestris.shogun.lib.util.FileUtil;
import de.terrestris.shogun.lib.util.ImageFileUtil;
import de.terrestris.shogun.properties.UploadProperties;
import org.apache.commons.io.IOUtils;
import org.apache.tomcat.util.http.fileupload.impl.InvalidContentTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

@Service
public class ImageFileService extends BaseFileService<ImageFileRepository, ImageFile> {

    @Autowired
    private UploadProperties uploadProperties;

    public ImageFile create(MultipartFile uploadFile) throws Exception {

        FileUtil.validateFile(uploadFile);

        byte[] fileByteArray = FileUtil.fileToByteArray(uploadFile);

        ImageFile file = new ImageFile();
        file.setFile(fileByteArray);
        file.setFileType(uploadFile.getContentType());
        file.setFileName(uploadFile.getOriginalFilename());
        file.setActive(true);

        Dimension imageDimensions = ImageFileUtil.getImageDimensions(uploadFile);
        if (imageDimensions != null) {
            int thumbnailSize = uploadProperties.getImage().getThumbnailSize();
            file.setThumbnail(ImageFileUtil.getScaledImage(uploadFile, imageDimensions, thumbnailSize));
            file.setWidth(imageDimensions.width);
            file.setHeight(imageDimensions.height);
        } else {
            LOG.warn("Could not detect the dimensions of the image. Neither width, height " +
                "nor the thumbnail can be set.");
        }

        ImageFile savedFile = this.create(file);

        return savedFile;
    }

    public void isValidType(String contentType) throws InvalidContentTypeException {
        if (uploadProperties == null) {
            throw new InvalidContentTypeException("No properties for the upload found. " +
                "Please check your application.yml");
        }

        if (uploadProperties.getImage() == null) {
            throw new InvalidContentTypeException("No properties for the image file upload found. " +
                "Please check your application.yml");
        }

        if (uploadProperties.getImage().getSupportedContentTypes() == null) {
            throw new InvalidContentTypeException("No list of supported content types for the image file upload found. " +
                "Please check your application.yml");
        }

        List<String> supportedContentTypes = uploadProperties.getImage().getSupportedContentTypes();

        boolean isMatch = PatternMatchUtils.simpleMatch(supportedContentTypes.toArray(new String[supportedContentTypes.size()]), contentType);

        if (!isMatch) {
            throw new InvalidContentTypeException("Unsupported content type for upload!");
        }
    }

    @Override
    public ImageFile create(MultipartFile uploadFile, Boolean writeToSystem) throws Exception {
        if (!writeToSystem) {
            return this.create(uploadFile);
        }

        String uploadBasePath = uploadProperties.getPath();
        if(uploadBasePath == null || uploadBasePath.isEmpty()) {
            throw new Exception("Could not upload file. uploadBasePath is null.");
        }
        String fileName = uploadFile.getOriginalFilename();
        if(fileName == null || fileName.isEmpty()) {
            throw new Exception("Could not upload file. fileName is null.");
        }

        FileUtil.validateFile(uploadFile);
        byte[] fileByteArray = FileUtil.fileToByteArray(uploadFile);
        ImageFile file = new ImageFile();
        file.setFileType(uploadFile.getContentType());
        file.setFileName(uploadFile.getOriginalFilename());
        file.setActive(true);

        Dimension imageDimensions = ImageFileUtil.getImageDimensions(uploadFile);
        if (imageDimensions != null) {
            int thumbnailSize = uploadProperties.getImage().getThumbnailSize();
            file.setThumbnail(ImageFileUtil.getScaledImage(uploadFile, imageDimensions, thumbnailSize));
            file.setWidth(imageDimensions.width);
            file.setHeight(imageDimensions.height);
        } else {
            LOG.warn("Could not detect the dimensions of the image. Neither width, height " +
                "nor the thumbnail can be set.");
        }

        ImageFile savedFile = this.create(file);
        UUID fileUuid = savedFile.getFileUuid();

        // Setup path and directory
        String path = fileUuid + "/" + fileName;
        java.io.File fileDirectory = new java.io.File(uploadBasePath + "/" + fileUuid);
        fileDirectory.mkdirs();

        // Write multipart file data to target directory
        java.io.File outFile = new java.io.File(fileDirectory, fileName);
        InputStream in = new ByteArrayInputStream(fileByteArray);

        try (OutputStream out = new FileOutputStream(outFile)) {
            IOUtils.copy(in, out);
            LOG.info("Saved file with id {} to {}: ", savedFile.getId(), savedFile.getPath());
        } catch (Exception e) {
            LOG.error("Error when saving file {} to disk: " + e.getMessage(), savedFile.getId());
            LOG.info("Rollback creation of file {}.", savedFile.getId());
            this.repository.delete(savedFile);
            throw e;
        }

        // Update entity with saved File
        savedFile.setPath(path);
        return this.repository.save(savedFile);
    }
}
