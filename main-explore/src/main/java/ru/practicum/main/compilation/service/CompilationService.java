package ru.practicum.main.compilation.service;

import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.dto.NewCompilationDto;
import ru.practicum.main.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    // Admin methods
    CompilationDto createCompilation(NewCompilationDto newCompilationDto);
    void deleteCompilation(Long compId);
    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest);

    // Public methods
    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);
    CompilationDto getCompilationById(Long compId);
}
