package com.apocaly.apocaly_path_private.notice.repository;

import com.apocaly.apocaly_path_private.notice.model.entity.UserNoticeLike;
import com.apocaly.apocaly_path_private.notice.model.entity.UserNoticeLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNoticeLikesRepository extends JpaRepository<UserNoticeLike, UserNoticeLikeId> {
}
