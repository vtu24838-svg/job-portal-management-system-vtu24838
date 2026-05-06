package com.careerconnect.dto;

import com.careerconnect.enums.PostType;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class NetworkPostDto {
    private String content;
    private PostType postType;
}
