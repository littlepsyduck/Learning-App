package com.example.learning_app.utils;

import com.example.learning_app.entities.Question;
import java.util.List;

public class LessonJson {
    public String name;
    public String imageRes;
    public int sectionId;
    public boolean isLocked;

    public List<Question> questions;
}