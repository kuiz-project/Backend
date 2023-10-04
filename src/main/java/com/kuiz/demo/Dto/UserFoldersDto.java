package com.kuiz.demo.Dto;

import com.kuiz.demo.model.User;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class UserFoldersDto {
    private Integer user_code;
    private List<FolderPdf> folderDtos;

    public UserFoldersDto(User user) {
        this.user_code = user.getUser_code();
        this.folderDtos = user.getFolders().stream()
                .map(FolderPdf::new)
                .collect(Collectors.toList());
    }
}