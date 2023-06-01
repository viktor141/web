package ru.vixtor.server;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.UploadContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MultiPartRequest extends Request implements UploadContext {

    private String body;

    private String boundary;

    public MultiPartRequest(Method method, String handled, String path, String body) throws FileUploadException, IOException {
        this.method = method;
        this.body = body;
        filePath = Path.of(".", "public", handled);

        try {
            List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(new URI(path), StandardCharsets.UTF_8);

            for (NameValuePair nameValuePair : nameValuePairs) {
                params.put(nameValuePair.getName(), nameValuePair.getValue());
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }


        boundary = body.substring(2, body.indexOf('\n')).trim();

        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> fileItems = upload.parseRequest(this);
        for (FileItem fileItem: fileItems) {
            if (fileItem.isFormField()){
                bodyParams.put((!bodyParams.containsKey(fileItem.getFieldName())) ? fileItem.getFieldName() : fileItem.getFieldName() + "*Duplicated", fileItem.getString());
            } else {
                File filePath = new File("run" + File.separator + "server");
                if(!filePath.exists()){
                    filePath.mkdirs();
                }

                Files.write(Path.of(filePath.getPath() + File.separator + fileItem.getName()), fileItem.get());
            }
        }
    }


    @Override
    public String getCharacterEncoding() {
        return "UTF-8";
    }

    @Override
    public String getContentType() {
        return "multipart/form-data, boundary=" + this.boundary;
    }

    @Override
    public int getContentLength() {
        return (int) contentLength();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(body.getBytes());
    }

    @Override
    public long contentLength() {
        return body.length();
    }
}
