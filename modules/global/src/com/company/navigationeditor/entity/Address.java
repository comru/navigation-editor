package com.company.navigationeditor.entity;

import javax.persistence.Embeddable;
import com.haulmont.chile.core.annotations.MetaClass;
import javax.persistence.Column;
import com.haulmont.cuba.core.entity.EmbeddableEntity;
import com.haulmont.chile.core.annotations.NamePattern;

@NamePattern("%s %s|city,country")
@MetaClass(name = "ne$Address")
@Embeddable
public class Address extends EmbeddableEntity {
    private static final long serialVersionUID = -293951847355857028L;

    @Column(name = "CITY")
    protected String city;

    @Column(name = "COUNTRY")
    protected String country;

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }


}