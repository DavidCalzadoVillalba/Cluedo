package es.us.dp1.L5_5_26_27.cluedo.notebook;

import jakarta.validation.constraints.NotNull;

public record NotebookMarkUpdateRequest(@NotNull NotebookMarkValue value) {
}
