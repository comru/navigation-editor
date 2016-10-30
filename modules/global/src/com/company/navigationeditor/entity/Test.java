package com.company.navigationeditor.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import com.haulmont.cuba.core.entity.StandardEntity;

@Table(name = "NE_TEST")
@Entity(name = "ne$Test")
public class Test extends StandardEntity {
    private static final long serialVersionUID = 8755788420840038287L;

    @Column(name = "NAME")
    protected String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


}