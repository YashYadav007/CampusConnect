package com.campusconnect.qa.repository;

import com.campusconnect.enums.VoteType;

public interface AnswerVoteCount {
  Long getAnswerId();
  VoteType getVoteType();
  Long getCnt();
}
