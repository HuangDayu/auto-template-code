package com.starcare.ecg.${entityName?uncap_first};

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.util.Date;
import com.nncs.iot.common.dto.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ${entityName}Entity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

<#list params as param>

    /**
     * ${param.fieldNote}
     */
    private ${param.fieldType} ${param.fieldName};

</#list>
<#list params as param>

    public void set${param.fieldName?cap_first}(${param.fieldType} ${param.fieldName}){
        this.${param.fieldName} = ${param.fieldName};
    }

    public ${param.fieldType} get${param.fieldName?cap_first}(){
        return this.${param.fieldName};
    }

</#list>
}