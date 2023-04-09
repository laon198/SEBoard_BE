package com.seproject.seboard.application;

import com.seproject.seboard.application.dto.comment.CommentCommand;
import com.seproject.seboard.application.dto.comment.CommentCommand.CommentEditCommand;
import com.seproject.seboard.application.dto.comment.CommentCommand.CommentListFindCommand;
import com.seproject.seboard.application.dto.comment.CommentCommand.CommentWriteCommand;
import com.seproject.seboard.application.dto.comment.ReplyCommand;
import com.seproject.seboard.application.dto.comment.ReplyCommand.ReplyEditCommand;
import com.seproject.seboard.application.dto.comment.ReplyCommand.ReplyWriteCommand;
import com.seproject.seboard.controller.dto.PaginationResponse;
import com.seproject.seboard.controller.dto.comment.CommentResponse.CommentListElement;
import com.seproject.seboard.controller.dto.comment.CommentResponse.CommentListResponse;
import com.seproject.seboard.controller.dto.comment.ReplyResponse;
import com.seproject.seboard.domain.model.comment.Comment;
import com.seproject.seboard.domain.model.comment.Reply;
import com.seproject.seboard.domain.model.exposeOptions.ExposeOption;
import com.seproject.seboard.domain.model.post.Post;
import com.seproject.seboard.domain.model.user.Anonymous;
import com.seproject.seboard.domain.model.user.Member;
import com.seproject.seboard.domain.repository.Page;
import com.seproject.seboard.domain.repository.PagingInfo;
import com.seproject.seboard.domain.repository.comment.CommentRepository;
import com.seproject.seboard.domain.repository.comment.CommentSearchRepository;
import com.seproject.seboard.domain.repository.comment.ReplyRepository;
import com.seproject.seboard.domain.repository.post.PostRepository;
import com.seproject.seboard.domain.repository.user.AnonymousRepository;
import com.seproject.seboard.domain.repository.user.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.seproject.seboard.application.utils.AppServiceHelper.findByIdOrThrow;

@Service
@RequiredArgsConstructor
public class CommentAppService {
    private final CommentRepository commentRepository;
    private final CommentSearchRepository commentSearchRepository;
    private final ReplyRepository replyRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final AnonymousRepository anonymousRepository;

    public void writeComment(CommentWriteCommand command){
        if(command.isAnonymous()){
            writeUnnamedComment(command);
        }else{
            writeNamedComment(command);
        }
    }
    protected void writeNamedComment(CommentWriteCommand command) {
        Post post = findByIdOrThrow(command.getPostId(), postRepository, "");
        Member member = memberRepository.findByAccountId(command.getAccountId());

        if (member == null) {
            //TODO : member 생성 로직 호출
        }

        //TODO : expose option 로직 추가
        Comment comment = post.writeComment(command.getContents(), member, ExposeOption.of(command.getExposeState(), command.getExposePassword()));

        commentRepository.save(comment);
    }


    @Transactional
    protected void writeUnnamedComment(CommentWriteCommand command) {
        Post post = findByIdOrThrow(command.getPostId(), postRepository, "");

        //TODO : JPQL로 변경?
        Anonymous author = getAnonymous(command.getAccountId(), command.getPostId(), post);

        //TODO : expose option 로직 추가
        Comment comment = post.writeComment(command.getContents(), author, ExposeOption.of(command.getExposeState(), command.getExposePassword()));

        commentRepository.save(comment);
    }

    public void writeReply(ReplyWriteCommand command){
        if(command.isAnonymous()){
            writeUnnamedReply(command);
        }else{
            writeNamedReply(command);
        }
    }

    protected void writeNamedReply(ReplyWriteCommand command) {
        Post post = findByIdOrThrow(command.getPostId(), postRepository, "");

        Member member = memberRepository.findByAccountId(command.getAccountId());
        Comment superComment = findByIdOrThrow(command.getSuperCommentId(), commentRepository, "");
        Comment taggedComment = findByIdOrThrow(command.getTagCommentId(), commentRepository, "");

        if (member == null) {
            //TODO : member 생성 로직 호출
        }

        Reply reply = superComment.writeReply(command.getContents(), taggedComment, member, ExposeOption.of(command.getExposeState(), command.getExposePassword()));

        commentRepository.save(reply);
    }


    @Transactional
    protected void writeUnnamedReply(ReplyWriteCommand command) {
        Post post = findByIdOrThrow(command.getPostId(), postRepository, "");
        Comment superComment = findByIdOrThrow(command.getSuperCommentId(), commentRepository, "");
        Comment taggedComment = findByIdOrThrow(command.getTagCommentId(), commentRepository, "");

        //TODO : JPQL로 변경?
        Anonymous author = getAnonymous(command.getAccountId(), command.getPostId(), post);

        Reply reply = superComment.writeReply(command.getContents(), taggedComment, author, ExposeOption.of(command.getExposeState(), command.getExposePassword()));

        replyRepository.save(reply);
    }

    public CommentListResponse retrieveCommentList(CommentListFindCommand command) {
        Page<Comment> commentPage = commentSearchRepository.findCommentByPostId(command.getPostId(), new PagingInfo(command.getPage(), command.getPerPage()));

        List<CommentListElement> commentDtoList = commentPage.getData().stream()
                .map(comment -> {
                            List<ReplyResponse> subComments = commentSearchRepository.findReplyByCommentId(comment.getCommentId())
                                    .stream()
                                    .map(
                                            reply -> ReplyResponse.toDto(reply, reply.isWrittenBy(command.getAccountId()))
                                    ).collect(Collectors.toList());

                    return CommentListElement.toDto(comment, comment.isWrittenBy(command.getAccountId()), subComments);
                })
                .collect(Collectors.toList());

        PaginationResponse paginationResponse = PaginationResponse.builder()
                .currentPage(commentPage.getCurPage())
                .contentSize(commentPage.getTotalSize())
                .perPage(commentPage.getPerPage())
                .lastPage(commentPage.getLastPage())
                .build();

        return CommentListResponse.toDto(commentDtoList, paginationResponse);
    }

    public void editComment(CommentEditCommand command) {
        Comment comment = findByIdOrThrow(command.getCommentId(), commentRepository, "");

        if (!comment.isWrittenBy(command.getAccountId())) { //TODO : 관리자 권한의 경우 생각
            throw new IllegalArgumentException();
        }

        comment.changeContents(comment.getContents());
        comment.changeExposeOption(ExposeOption.of(command.getExposeState(), command.getExposePassword()));
    }

    public void editReply(ReplyEditCommand command) {
        Reply reply = findByIdOrThrow(command.getReplyId(), replyRepository, "");

        if (!reply.isWrittenBy(command.getAccountId())) { //TODO : 관리자 권한의 경우 생각
            throw new IllegalArgumentException();
        }

        reply.changeContents(command.getContents());
        reply.changeExposeOption(ExposeOption.of(command.getExposeState(), command.getExposePassword()));
    }

    public void removeComment(Long commentId, Long accountId) {
        Comment comment = findByIdOrThrow(commentId, commentRepository, "");

        if (!comment.isWrittenBy(accountId)) { //TODO : 관리자 권한의 경우 생각
            throw new IllegalArgumentException();
        }

        comment.delete();
    }

    public void removeReply(Long replyId, Long accountId) {
        Reply reply = findByIdOrThrow(replyId, replyRepository, "");

        if (!reply.isWrittenBy(accountId)) { //TODO : 관리자 권한의 경우 생각
            throw new IllegalArgumentException();
        }

        reply.delete();
    }

    private Anonymous getAnonymous(Long accountId, Long postId, Post post) {
        Anonymous author = commentRepository.findByPostId(postId).stream()
                .filter(comment -> comment.getAuthor().isAnonymous())
                .map(comment -> (Anonymous) comment.getAuthor())
                .filter(anonymous -> anonymous.isOwnAccountId(accountId))
                .findFirst()
                .orElseGet(() -> {
                    return replyRepository.findByPostId(postId).stream()
                            .filter(reply -> reply.getAuthor().isAnonymous())
                            .map(reply -> (Anonymous) reply.getAuthor())
                            .filter(anonymous -> anonymous.isOwnAccountId(accountId))
                            .findFirst()
                            .orElseGet(() -> {
                                Anonymous createdAnonymous = post.createAnonymous(accountId);
                                anonymousRepository.save(createdAnonymous);
                                return createdAnonymous;
                            });
                });
        return author;
    }

}
