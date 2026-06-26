package com.digitaldetox.service;

import com.digitaldetox.core.exceptions.EntityNotFoundException;
import com.digitaldetox.core.exceptions.FileUploadException;
import com.digitaldetox.dto.attachment.AttachmentFileDTO;
import com.digitaldetox.dto.attachment.AttachmentReadOnlyDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IAttachmentService {

    AttachmentReadOnlyDTO uploadCheckInAttachment(UUID checkInUuid, MultipartFile file)
            throws EntityNotFoundException, FileUploadException;

    List<AttachmentReadOnlyDTO> getCheckInAttachments(UUID checkInUuid) throws EntityNotFoundException;

    AttachmentFileDTO downloadCheckInAttachment(UUID checkInUuid, UUID attachmentUuid)
            throws EntityNotFoundException, FileUploadException;
}
