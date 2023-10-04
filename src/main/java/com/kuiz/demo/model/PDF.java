package com.kuiz.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class PDF {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pdf_id;

    @Column
    private String file_url;

    @Column (nullable = false)
    private String file_name;

    @Column (nullable = false)
    private String subject;

    @Column(columnDefinition = "JSON")
    private String keywords;

    @ManyToOne(fetch = FetchType.LAZY) //Many = Folder, One = User
    @JoinColumn(name="folder_id", referencedColumnName="folder_id")
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_code", referencedColumnName="user_code", nullable = false)
    private User user;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="pdf")
    private List<Test> tests;
}
