import org.gradle.api.Project
import kotlin.reflect.KProperty

class AuthDelegate private constructor(private val delegate: Project) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>) =
        delegate.findProperty(property.name) as? String

    companion object {
        val Project.auth get() = AuthDelegate(this)
    }

}