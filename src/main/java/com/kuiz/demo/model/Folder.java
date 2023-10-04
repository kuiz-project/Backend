package com.kuiz.demo.model;

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
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer folder_id;

    @Column(nullable = false)
    private String folder_name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="folder")
    private List<PDF> pdfs;

    @ManyToOne(fetch = FetchType.LAZY) //Many = Folder, One = User
    @JoinColumn(name="user_code", referencedColumnName="user_code", nullable = false)
    private User user;
}