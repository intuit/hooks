import org.gradle.api.Project
import kotlin.reflect.KProperty

class AuthDelegate private constructor(private val delegate: Project, private val name: String? = null, private val transform: (String?) -> String? = { it }) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>) =
        transform(delegate.findProperty(name ?: property.name) as? String)

    companion object {
        val Project.auth get() = AuthDelegate(this)
        fun Project.auth(name: String? = null, transform: (String?) -> String? = { it }) = AuthDelegate(this, name, transform)
    }

}