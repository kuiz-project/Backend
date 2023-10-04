package com.kuiz.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert //insert할때 null 제외
@Entity(name = "users") //User 클래스가 MySQL에 테이블 생성
public class User {

    @Id  //Primary key
    @Column(name="user_code")
    @GeneratedValue(strategy = GenerationType.IDENTITY) //프로젝트에서 연결된 DB의 넘버링 전략을 따라감
    private Integer user_code; //my_sql auto_increment

    @Column (nullable = false, length = 10)
    private String name; //이름

    @Column (nullable = false, unique = true)
    private String identifier;

    @Column (nullable = false)
    private String password; //비밀번호

    @Column
    private String email;


    @OneToMany(fetch = FetchType.LAZY, mappedBy="user")
    private List<Folder> folders;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="user")
    private List<Test> tests;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="user")
    private List<PDF> pdfs;

}
