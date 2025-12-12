package com.codegen.model;

public enum RequirementStatus {
    PENDING,      //ожидает обработки
    ANALYZING,    //в процессе анализа
    GENERATING,   //генерируется код
    VALIDATING,   //проверка
    COMPLETED,    //завершено
    FAILED        // Ошибка
}