package xyz.fycz.myreader.model.third3.analyzeRule

interface RuleDataInterface {

    val variableMap: Map<String, String>

    fun putVariable(key: String, value: String?)

    fun getVariable(key: String): String? {
        return variableMap[key]
    }

}