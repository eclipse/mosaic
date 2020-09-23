<#function formatLicenseList licenses>
    <#assign result = ""/>
    <#list licenses as license>
        <#assign result = result + license + ", " />
    </#list>
    <#assign filteredResult = result?substring(0, result?last_index_of(',')) />
    <#return filteredResult>
</#function>
<#function formatDependency e>
    <#assign p = e.getKey()/>
    <#assign licenses = e.getValue()/>
    <#return p.name + " (" + p.version + ")"
    + "\n\n * License: " + formatLicenseList(licenses)
    + "\n * Maven artifact: `" + p.groupId + ":" + p.artifactId + ":" + p.version + "`"
    + "\n * Project: " + ((p.url)!"not available")
    + "\n * Source: " + ((p.scm.url)!"not declared")
        ?replace('(git@|scm:git:git://|git://|http://)','https://','r')
        ?replace('.git','')
        ?replace('https://github.com:','https://github.com/')
    + "\n\n">
</#function>
# Third-party Content

<#list dependencyMap as e>
${formatDependency(e)}
</#list>