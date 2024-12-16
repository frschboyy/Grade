package com.gradingsystem.tesla.DTO;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationDetails {
    int grade;
    int plagiarismScore;
    Map<String, String> results;
}
