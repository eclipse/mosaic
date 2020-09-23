<#function formatLicenseList licenses>
    <#assign result = ""/>
    <#list licenses as license>
        <#assign result = result + license + "," />
    </#list>
    <#assign filteredResult = result?substring(0, result?last_index_of(',')) />
    <#return filteredResult>
</#function>
<#function formatDependency e>
    <#assign p = e.getKey()/>
    <#assign licenses = e.getValue()/>
    <#return p.name + ";" + p.groupId + ";" + p.artifactId + ";" + p.version + ";" + formatLicenseList(licenses)>
</#function>
<#list dependencyMap as e>
${formatDependency(e)}
</#list>