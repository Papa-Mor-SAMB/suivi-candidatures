package com.papa.suivicandidatures.controller;

import com.papa.suivicandidatures.model.Candidature;
import com.papa.suivicandidatures.model.Statut;
import com.papa.suivicandidatures.service.CandidatureService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidatures")
public class CandidatureController {

    private final CandidatureService candidatureService;

    public CandidatureController(CandidatureService candidatureService) {
        this.candidatureService = candidatureService;
    }

    @GetMapping
    public List<Candidature> getAll(@RequestParam(required = false) Statut statut, Authentication auth) {
        return candidatureService.getAll(auth.getName(), statut);
    }

    @GetMapping("/stats")
    public Map<String, Integer> getStats(Authentication auth) {
        return candidatureService.getStats(auth.getName());
    }

    @GetMapping("/relances")
    public List<Candidature> getRelances(Authentication auth) {
        return candidatureService.getRelances(auth.getName());
    }

    @PostMapping("/{id}/relancer")
    public Candidature relancer(@PathVariable Long id, Authentication auth) {
        return candidatureService.marquerRelancee(auth.getName(), id);
    }

    @GetMapping("/{id}")
    public Candidature getById(@PathVariable Long id, Authentication auth) {
        return candidatureService.getById(auth.getName(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Candidature create(@Valid @RequestBody Candidature candidature, Authentication auth) {
        return candidatureService.create(auth.getName(), candidature);
    }

    @PutMapping("/{id}")
    public Candidature update(@PathVariable Long id, @Valid @RequestBody Candidature candidature, Authentication auth) {
        return candidatureService.update(auth.getName(), id, candidature);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        candidatureService.delete(auth.getName(), id);
    }
}
