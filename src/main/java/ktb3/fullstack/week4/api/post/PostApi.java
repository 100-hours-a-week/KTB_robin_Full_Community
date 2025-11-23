package ktb3.fullstack.week4.api.post;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ktb3.fullstack.week4.Security.context.SecurityUser;
import ktb3.fullstack.week4.dto.common.ApiResponse;
import ktb3.fullstack.week4.dto.posts.PostDetailResponse;
import ktb3.fullstack.week4.dto.posts.PostEditRequest;
import ktb3.fullstack.week4.dto.posts.PostListResponse;
import ktb3.fullstack.week4.dto.posts.PostUploadRequeset;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "[게시글 API]")
public interface PostApi {

    @Operation(summary = "게시글 등록", description = "새로운 게시글을 추가합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "게시글 등록 성공", value = """
                            {
                                "message" : "post_upload_success",
                                "data" : null
                            }
                            """),
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "이미지 용량이 너무 큽니다.", value = """
                            {
                                "message" : "image_size_too_big",
                                "data" : null
                            }
                            """),
                            @ExampleObject(name = "첨부한 파일의 타입은 지원하지 않습니다.", value = """
                            {
                                "message" : "invalid_file_type",
                                "data" : null
                            }
                            """)
                    })
            ),
    })
    ApiResponse<Void> uploadPost(
            @Parameter(hidden = true) @AuthenticationPrincipal SecurityUser user,
            @Parameter(
                    name = "dto",
                    description = "게시글 내용(JSON)",
                    required = true,
                    content = @Content(mediaType = "application/json")
            )
            @Valid @RequestPart PostUploadRequeset dto,
            @Parameter(
                    name = "image",
                    description = "게시글 이미지 파일(선택)",
                    required = false,
                    content = @Content(mediaType = "multipart/form-data")
            )
            @RequestPart(required = false) MultipartFile image
    );

    @Operation(summary = "게시글 목록 조회", description = "게시글 목록을 가져옵니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "게시글 목록 조회 성공", value = """
                                {
                                    "message" : "posts_fetch_success",
                                    "data" : {
                                        "posts": [
                                             {
                                                  "id": 1
                                                  "title": "title1",
                                                  "likes": 9876,
                                                  "comments": 200,
                                                  "views": 12000,
                                                  "author": "robin",
                                                  "modified_at": "2025-09-30T20:30:15+09:00"
                                             },
                                             {
                                                  "id": 2
                                                  "title": "title2",
                                                  "likes": 1234,
                                                  "comments": 300,
                                                  "views": 15000,
                                                  "author": "robin2",
                                                  "modified_at": "2025-10-01T20:30:15+09:00"
                                             },
                                             . . .
                                        ],
                                        "next_cursor": 6,  // 클라이언트에서 after의 값을 next_cursor-1 로 보내야합니다.
                                        "has_next": true
                                    }
                                }
                                """)
                    })
            )
    })
    ApiResponse<PostListResponse> getPostList(
            @Parameter(hidden = true) @AuthenticationPrincipal SecurityUser user,
            @RequestParam(value = "after") int after,
            @RequestParam(value = "limit") int limit
    );


    @Operation(summary = "게시글 상세 조회", description = "게시글 하나를 상세 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "게시글 상세 조회 성공", value = """
                                {
                                     "message" : "post_fetch_success",
                                     "data" : {
                                         "post": {
                                                  "id": 1,
                                                   "title": "title1",
                                                   "likes": 9876,
                                                   "comments": 200,
                                                   "views": 12000,
                                                   "author": "robin",
                                                   "modified_at": "2025-09-30T20:30:15+09:00",
                                                   "image_url": "http://www.example.com/images/image1234.jpg"
                                          },
                                          "liked": false,
                                          "owner": false,
                                          "comments": [
                                                {
                                                    "id": 1,
                                                    "author": "bluer",
                                                    "content": "good posting",
                                                    "modified_at": "2025-10:02T20:30:15+09:00"
                                                },
                                                {
                                                    "id": 2,
                                                    "author": "brian",
                                                    "content": "awesome posting",
                                                    "modified_at": "2025-10:01T20:30:15+09:00"
                                                },
                                                . . .
                                          ]
                                     }
                                 }
                                """),
                        })
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "조회하려는 게시글의 id가 유효하지 않습니다.", value = """
                                {
                                    "message" : "post_id_is_invalid",
                                    "data" : null
                                }
                                """),
                        })
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "해당 게시글을 찾을 수 없습니다.", value = """
                                {
                                    "message" : "cannot_found_post",
                                    "data" : null
                                }
                                """),
                        })
                )
        })
        ApiResponse<PostDetailResponse> getPostDetail(
                @Parameter(hidden = true) @AuthenticationPrincipal SecurityUser user,
                @PathVariable(value = "id") long postId
        );


        @Operation(summary = "게시글 수정", description = "게시글의 정보를 수정합니다.")
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "게시글 수정 성공", value = """
                                {
                                    "message" : "post_edit_success",
                                    "data" : null
                                }
                                """),
                        })
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "이미지 용량이 너무 큽니다.", value = """
                                {
                                    "message" : "image_size_too_big",
                                    "data" : null
                                }
                                """),
                                @ExampleObject(name = "첨부한 파일의 타입은 지원하지 않습니다.", value = """
                                {
                                    "message" : "invalid_file_type",
                                    "data" : null
                                }
                                """)
                        })
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "다른 사람의 게시글은 수정할 수 없습니다.", value = """
                                {
                                    "message" : "cannot_edit_others_post",
                                    "data" : null
                                }
                                """),
                        })
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "해당 게시글을 찾을 수 없습니다.", value = """
                                {
                                    "message" : "cannot_found_post",
                                    "data" : null
                                }
                                """),
                        })
                )
        })
        ApiResponse<Void> editPost(
                @Parameter(hidden = true) @AuthenticationPrincipal SecurityUser user,
                @PathVariable("id") long postId,
                @Parameter(
                        name = "dto",
                        description = "게시글 내용(JSON)",
                        required = true,
                        content = @Content(mediaType = "application/json")
                )
                @Valid @RequestPart PostEditRequest dto,
                @Parameter(
                        name = "image",
                        description = "게시글 이미지 파일(선택)",
                        required = false,
                        content = @Content(mediaType = "multipart/form-data")
                )
                @RequestPart(required = false) MultipartFile image
        );

        @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "204",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "게시글 삭제 성공", value = """
                                {
                                    "message" : "post_delete_success",
                                    "data" : null
                                }
                                """),
                        })
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "다른 사람의 게시글은 삭제할 수 없습니다.", value = """
                                {
                                    "message" : "cannot_delete_others_post",
                                    "data" : null
                                }
                                """),
                        })
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "해당 게시글을 찾을 수 없습니다.", value = """
                                {
                                    "message" : "cannot_found_post",
                                    "data" : null
                                }
                                """),
                        })
                )
        })
        ApiResponse<Void> removePost(
                @Parameter(hidden = true) @AuthenticationPrincipal SecurityUser user,
                @PathVariable("id") long postId
        );

        @Operation(summary = "게시글 좋아요", description = "게시글에 좋아요를 누릅니다.")
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "게시글 좋아요 성공", value = """
                                {
                                    "message" : "like_success",
                                    "data" : {
                                        "likes": 9877,
                                        "liked": true
                                    }
                                }
                                """),
                        })
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "해당 게시글을 찾을 수 없습니다.", value = """
                                {
                                    "message" : "cannot_found_post",
                                    "data" : null
                                }
                                """),
                        })
                )
        })
        ApiResponse<Void> addLike(
                @Parameter(hidden = true) @AuthenticationPrincipal SecurityUser user,
                @PathVariable("id") long postId
        );

        @Operation(summary = "게시글 좋아요 취소", description = "게시글에 누른 좋아요를 취소합니다.")
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "게시글 좋아요 취소", value = """
                                {
                                    "message" : "unlike_success",
                                    "data" :  {
                                        "likes": 9875,
                                        "liked": false
                                     }
                                }
                                """),
                        })
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "해당 게시글을 찾을 수 없습니다.", value = """
                                {
                                    "message" : "cannot_found_post",
                                    "data" : null
                                }
                                """),
                        })
                )
        })
        ApiResponse<Void> removeLike(
                @Parameter(hidden = true) @AuthenticationPrincipal SecurityUser user,
                @PathVariable("id") long postId
        );


        @Operation(summary = "게시글 댓글 등록", description = "게시글에 댓글을 씁니다.")
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "201",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "게시글에 댓글 작성 성공", value = """
                                {
                                    "message" : "comment_upload_success",
                                    "data" : null
                                }
                                """),
                        })
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "댓글의 길이가 너무 깁니다.", value = """
                                {
                                    "message" : "comment_is_too_long",
                                    "data" : null
                                }
                                """),
                        })
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "해당 게시글을 찾을 수 없습니다.", value = """
                                {
                                    "message" : "cannot_found_post",
                                    "data" : null
                                }
                                """),
                        })
                )
        })
        ApiResponse<Void> addComment(
                @Parameter(hidden = true) @AuthenticationPrincipal SecurityUser user,
                @PathVariable("postId") long postId,
                @RequestParam("content") String content
        );

        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "댓글 수정 성공", value = """
                                {
                                    "message" : "comment_edit_success",
                                    "data" : {
                                        "content": "new_comment1",
                                        "modified_at": "2025-10-02T20:30:15+09:00"
                                     }
                                }
                                """),
                        })
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "댓글의 길이가 너무 깁니다.", value = """
                                {
                                    "message" : "comment_is_too_long",
                                    "data" : null
                                }
                                """),
                        })
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "다른 사람의 댓글을 수정할 수 없습니다.", value = """
                                {
                                    "message" : "cannot_edit_others_comment",
                                    "data" : null
                                }
                                """),
                        })
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = " 게시글을 찾을 수 없습니다.", value = """
                                {
                                    "message" : "cannot_found_post",
                                    "data" : null
                                }
                                """),
                        })
                )
        })
        @Operation(summary = "게시글 댓글 수정", description = "게시글에 쓴 댓글을 수정합니다.")
        ApiResponse<Void> editComment(
                @Parameter(hidden = true) @AuthenticationPrincipal SecurityUser user,
                @PathVariable("postId") long postId,
                @PathVariable("commentId") long commentId,
                @RequestParam("content") String content
        );

        @Operation(summary = "게시글 댓글 삭제", description = "게시글에 쓴 댓글을 삭제합니다.")
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "204",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "댓글 삭제 성공", value = """
                                {
                                    "message" : "comment_delete_success",
                                    "data" : null
                                }
                                """),
                        })
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "다른 사람의 댓글을 삭제할 수 없습니다.", value = """
                                {
                                    "message" : "cannot_delete_others_comment",
                                    "data" : null
                                }
                                """),
                        })
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        content = @Content(mediaType = "application/json", examples = {
                                @ExampleObject(name = "해당 게시글을 찾을 수 없습니다.", value = """
                                {
                                    "message" : "cannot_found_post",
                                    "data" : null
                                }
                                """),
                                @ExampleObject(name = "해당 댓글을 찾을 수 없습니다.", value = """
                                {
                                    "message" : "cannot_found_comment",
                                    "data" : null
                                }
                                """)
                        })
                ),
        })
        ApiResponse<Void> removeComment(
                @Parameter(hidden = true) @AuthenticationPrincipal SecurityUser user,
                @PathVariable("postId") long postId,
                @PathVariable("commentId") long commentId
        );
}