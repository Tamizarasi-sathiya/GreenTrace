package com.greentrace.greentrace.controller;

import com.greentrace.greentrace.model.Project;
import com.greentrace.greentrace.model.NdviRecord;
import com.greentrace.greentrace.repository.ProjectRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin
public class ProjectController {

    private final ProjectRepository projectRepository;

    public ProjectController(ProjectRepository projectRepository){
        this.projectRepository = projectRepository;
    }

    @GetMapping
    public List<Project> getAllProjects(){
        return projectRepository.findAll();
    }

    @PostMapping
    public Project createProject(@RequestBody Project project){
        return projectRepository.save(project);
    }

    // ------------------------------------
    // NDVI HISTORY ENDPOINT
    // ------------------------------------

    @GetMapping("/{id}/ndvi")
    public List<NdviRecord> getNdviHistory(@PathVariable String id){

        Project project = projectRepository.findById(id).orElse(null);

        if(project == null){
            return null;
        }

        return project.getNdviHistory();
    }
}