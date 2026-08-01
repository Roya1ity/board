package com.example.board.reaction;

import com.example.board.Global.Entity.Comment;
import com.example.board.Global.Entity.CommentReaction;
import com.example.board.Global.Entity.User;
import com.example.board.Global.exception.BusinessException;
import com.example.board.Global.exception.ErrorCode;
import com.example.board.auth.UserRepository;
import com.example.board.comment.CommentRepository;
import com.example.board.reaction.dto.ReactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentReactionServiceTest {

    @Mock
    private CommentReactionRepository commentReactionRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;

    private CommentReactionService commentReactionService;

    @BeforeEach
    void setUp() {
        commentReactionService = new CommentReactionService(
                commentReactionRepository,
                commentRepository,
                userRepository
        );
    }

    @Test
    void createsAReactionWhenTheUserHasNotReacted() {
        Comment comment = Comment.builder().id(10L).build();
        User user = User.builder().id(20L).build();
        AtomicReference<CommentReaction> savedReaction = new AtomicReference<>();
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        when(userRepository.findById(20L)).thenReturn(Optional.of(user));
        when(commentReactionRepository.findByCommentIdAndUserId(10L, 20L))
                .thenAnswer(invocation -> Optional.ofNullable(savedReaction.get()));
        when(commentReactionRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(CommentReaction.class)))
                .thenAnswer(invocation -> {
                    CommentReaction reaction = invocation.getArgument(0);
                    savedReaction.set(reaction);
                    return reaction;
                });
        when(commentReactionRepository.countByCommentIdAndType(10L, ReactionType.LIKE)).thenReturn(1L);
        when(commentReactionRepository.countByCommentIdAndType(10L, ReactionType.DISLIKE)).thenReturn(0L);

        ReactionResponse response = commentReactionService.reactToComment(10L, 20L, ReactionType.LIKE);

        ArgumentCaptor<CommentReaction> captor = ArgumentCaptor.forClass(CommentReaction.class);
        verify(commentReactionRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getComment()).isSameAs(comment);
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getType()).isEqualTo(ReactionType.LIKE);
        assertThat(response.getLikeCount()).isEqualTo(1L);
        assertThat(response.getDislikeCount()).isZero();
        assertThat(response.getMyReaction()).isEqualTo(ReactionType.LIKE);
    }

    @Test
    void removesAReactionWhenTheSameTypeIsRequestedAgain() {
        Comment comment = Comment.builder().id(10L).build();
        User user = User.builder().id(20L).build();
        CommentReaction reaction = CommentReaction.builder()
                .comment(comment)
                .user(user)
                .type(ReactionType.LIKE)
                .build();
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        when(userRepository.findById(20L)).thenReturn(Optional.of(user));
        when(commentReactionRepository.findByCommentIdAndUserId(10L, 20L))
                .thenReturn(Optional.of(reaction), Optional.empty());

        ReactionResponse response = commentReactionService.reactToComment(10L, 20L, ReactionType.LIKE);

        verify(commentReactionRepository).delete(reaction);
        verify(commentReactionRepository).flush();
        verify(commentReactionRepository, never()).saveAndFlush(reaction);
        assertThat(response.getMyReaction()).isNull();
    }

    @Test
    void changesAnExistingReactionToTheRequestedType() {
        Comment comment = Comment.builder().id(10L).build();
        User user = User.builder().id(20L).build();
        CommentReaction reaction = CommentReaction.builder()
                .comment(comment)
                .user(user)
                .type(ReactionType.LIKE)
                .build();
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        when(userRepository.findById(20L)).thenReturn(Optional.of(user));
        when(commentReactionRepository.findByCommentIdAndUserId(10L, 20L))
                .thenReturn(Optional.of(reaction));

        ReactionResponse response = commentReactionService.reactToComment(10L, 20L, ReactionType.DISLIKE);

        verify(commentReactionRepository).saveAndFlush(reaction);
        assertThat(reaction.getType()).isEqualTo(ReactionType.DISLIKE);
        assertThat(response.getMyReaction()).isEqualTo(ReactionType.DISLIKE);
    }

    @Test
    void rejectsAReactionForAnUnknownComment() {
        when(commentRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentReactionService.reactToComment(10L, 20L, ReactionType.LIKE))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);

        verify(userRepository, never()).findById(20L);
    }
}
