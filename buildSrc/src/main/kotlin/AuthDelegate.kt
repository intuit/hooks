import org.gradle.api.Project
import kotlin.reflect.KProperty

class AuthDelegate private constructor(private val delegate: Project, private val name: String? = null) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>) =
        delegate.findProperty(name ?: property.name) as? String

    companion object {
        val Project.auth get() = AuthDelegate(this)
        fun Project.auth(name: String? = null) = AuthDelegate(this, name)
    }

}