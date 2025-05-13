package org.rusak.rtu.ditef.ai.tsq.hw3.models;

import java.util.List;

public class PetVO {
    private Long id;
    private String name;
    private List<String> photoUrls;
    private List<TagVO> tags;
    private String status;
    private CategoryVO category;
    
    public boolean hasId() { return this.id != null; }
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls; }
    
    public List<TagVO> getTags() { return tags; }
    public void setTags(List<TagVO> tags) { this.tags = tags; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public CategoryVO getCategory() { return category; }
    public void setCategory(CategoryVO category) { this.category = category; }
}