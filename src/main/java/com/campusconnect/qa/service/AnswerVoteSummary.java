package com.campusconnect.qa.service;

public record AnswerVoteSummary(long upvotes, long downvotes) {
  public static AnswerVoteSummary zero() {
    return new AnswerVoteSummary(0L, 0L);
  }

  public long score() {
    return upvotes - downvotes;
  }
}

