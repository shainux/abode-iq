package io.sh.abodeiq.model

enum class NetworkProtocol(val key: String, val label: String) {
    ZWave("zwave","Z-Wave"),
    Manual("manual","Manual"),
    Ble("ble", "Bluetooth Low Energy (BLE)");

    companion object{
        fun findForName(name: String) = NetworkProtocol
            .values()
            .first { it.name.lowercase() == name.lowercase().filter(Char::isLetter) }
    }
}
