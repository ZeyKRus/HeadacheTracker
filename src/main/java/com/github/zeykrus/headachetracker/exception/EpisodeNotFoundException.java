package com.github.zeykrus.headachetracker.exception;

public class EpisodeNotFoundException extends RuntimeException {
    public EpisodeNotFoundException(String message) {
        super(message);
    }
}
