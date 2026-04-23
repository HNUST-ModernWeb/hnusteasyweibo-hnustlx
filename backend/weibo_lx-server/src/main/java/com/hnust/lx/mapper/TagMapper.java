package com.hnust.lx.mapper;

import com.hnust.lx.entity.Tag;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TagMapper {

    @Insert("INSERT INTO tag(tag_name, is_deleted) VALUES(#{tagName}, #{isDeleted})")
    @Options(useGeneratedKeys = true, keyProperty = "tagId")
    int insert(Tag tag);

    @Select("SELECT tag_id as tagId, tag_name as tagName, is_deleted as isDeleted " +
            "FROM tag WHERE is_deleted = 0 ORDER BY tag_id DESC")
    List<Tag> findAll();

    @Select("SELECT tag_id as tagId, tag_name as tagName, is_deleted as isDeleted " +
            "FROM tag WHERE is_deleted = 0 ORDER BY tag_id DESC")
    List<Tag> findByPage();

    @Select("SELECT tag_id as tagId, tag_name as tagName, is_deleted as isDeleted " +
            "FROM tag WHERE tag_id = #{tagId}")
    Tag findById(Long tagId);

    @Select("SELECT tag_id as tagId, tag_name as tagName, is_deleted as isDeleted " +
            "FROM tag WHERE tag_name = #{tagName}")
    Tag findByName(String tagName);

    @Update("UPDATE tag SET is_deleted = 1 WHERE tag_id = #{tagId}")
    int delete(Long tagId);

    @Select("SELECT COUNT(*) FROM tag WHERE is_deleted = 0")
    long count();
}
