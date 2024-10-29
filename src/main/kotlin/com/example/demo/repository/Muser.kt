package com.example.demo.repository

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * SpringBootとJPAを使ったEntityクラス
 * @Entity：Entityクラスであることを宣言する
 * @Table：name属性で連携するテーブル名を指定する
 */
@Entity
@Table(name = "M_USER")
data class Muser(
    /**
     *  @Id：主キーに指定する。※複合キーの場合は@EmbeddedIdを使用
     *  @GeneratedValue：主キーの指定をJPAに委ねる
     *  @Column：name属性でマッピングするカラム名を指定する
     */
    @Id
    @Column(name="INTERNAL_ID")
    var internalId: String,

    @Column(name="USER_ID")
    var userId: String,

    @Column(name="DISPLAY_NAME")
    var displayName: String,

    @Column(name="PASSWORD")
    var password: String,
)
