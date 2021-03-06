import org.spica.javaclient.actions.ActionContext
import org.spica.javaclient.model.Model
import org.spica.javaclient.model.TaskInfo
import org.spica.javaclient.services.Services

ActionContext actionContext = spica

Model model = actionContext.model
//Use services
Services services = actionContext.services

//Use api
/**UserInfo userInfo = actionContext.api.userApi.findUser("test")

println ("Users firstname " + userInfo.firstname)
println ("Users lastname " + userInfo.name)
println ("Users mail " + userInfo.email)**/

println "Proxy-Host: " + System.getProperty("http.proxyHost")

println model.projectInfos.size() + " projects available"
println model.userInfos.size() + " users available"
println model.taskInfos.size() + " topics available"

Collection<TaskInfo> topicInfos = model.taskInfos

//Make decisions due to model content
if (topicInfos.find {it.name.contains("Remove")}) {
    println "Task which starts with Remove exists"
}
else {
    println "Task which starts with Remove does not exists"
}

if (topicInfos.find {it.name.contains("xyz")}) {
    println "Task which starts with xyz exists"
}
else {
    println "Task which starts with xyz does not exist"
}

//Trigger actions when http site content changes (new versions of something)
boolean siteChanged = services.downloadService.siteChanged("http://someSillySite.com");
println "Site changed: " + siteChanged

//Get versions from text of tag of an http page
List<String> ideaVersions = services.downloadService.getOwnTextOfTags("https://confluence.jetbrains.com/collector/pages.action?key=IDEADEV", "a", "href", "Release");
println "Current version of Idea: " + String.join("\n", ideaVersions);

//Get all versions from attribute of tag of an http page
List<String> gradleVersions = services.downloadService.getAttributeOfTags("https://gradle.org/releases/", "a", "name", null)
println "All versions of gradle: " + String.join("\n", gradleVersions);
services.mailService.sendMail("Spica wants to speek with you", "Gradle versions: " + gradleVersions, Arrays.asList("TODO"))

