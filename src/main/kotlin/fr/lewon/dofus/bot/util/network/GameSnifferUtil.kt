package fr.lewon.dofus.bot.util.network

import fr.lewon.dofus.bot.model.characters.DofusCharacter
import fr.lewon.dofus.bot.scripts.CancellationToken
import fr.lewon.dofus.bot.sniffer.DofusMessageReceiver
import fr.lewon.dofus.bot.util.JNAUtil
import fr.lewon.dofus.bot.util.filemanagers.CharacterManager
import fr.lewon.dofus.bot.util.io.WaitUtil
import java.io.BufferedReader
import java.io.InputStreamReader

object GameSnifferUtil {

    private const val NETSTAT_COMMAND = "cmd.exe /c netstat -aop TCP -n | findstr :5555"
    private val NETSTAT_DOFUS_REGEX = Regex(
        "[\\s]+TCP" +
                "[\\s]+([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+):([0-9]+)" +
                "[\\s]+([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+):(5555)" +
                "[\\s]+ESTABLISHED" +
                "[\\s]+([0-9]+)"
    )
    private val FRAME_NAME_REGEX = Regex("(.*?) - Dofus.*?")

    private val connectionByCharacter = HashMap<DofusCharacter, DofusConnection>()
    private val snifferByPid = HashMap<Long, DofusMessageReceiver>()
    private val gameInfoByCharacter = HashMap<DofusCharacter, GameInfo>()

    fun updateNetwork() {
        synchronized(gameInfoByCharacter) {
            synchronized(snifferByPid) {
                synchronized(connectionByCharacter) {
                    updateDofusConnections()
                }
                updateSniffers()
            }
            updateGameInfo()
        }
    }

    private fun updateDofusConnections() {
        val findServerIpProcessBuilder = ProcessBuilder(NETSTAT_COMMAND.split(" "))
        val process = findServerIpProcessBuilder.start()
        process.waitFor()
        connectionByCharacter.clear()
        val dofusConnections = BufferedReader(InputStreamReader(process.inputStream)).readLines()
            .mapNotNull { NETSTAT_DOFUS_REGEX.matchEntire(it) }
            .map { parseDofusConnection(it) }

        for (dofusConnection in dofusConnections) {
            val characterName = getCharacterNameFromFrame(dofusConnection.pid) ?: continue
            val character = CharacterManager.getCharacterByName(characterName) ?: continue
            connectionByCharacter[character] = dofusConnection
        }
    }

    private fun parseDofusConnection(matchResult: MatchResult): DofusConnection {
        return DofusConnection(
            matchResult.groupValues[1].trim(),
            matchResult.groupValues[2].trim(),
            matchResult.groupValues[3].trim(),
            matchResult.groupValues[4].trim(),
            matchResult.groupValues[5].trim().toLong()
        )
    }

    private fun updateSniffers() {
        val runningConnections = connectionByCharacter.values
        val runningConnectionsPIDs = runningConnections.map { it.pid }
        removeDeadSniffers(snifferByPid.entries.filter { !runningConnectionsPIDs.contains(it.key) })
        startNewSniffers(runningConnections.filter { snifferByPid[it.pid] == null })

        if (!WaitUtil.waitUntil({ allSnifferRunning() }, CancellationToken(), 5000)) {
            error("Couldn't initialize sniffers")
        }
    }

    private fun updateGameInfo() {
        snifferByPid.map { it.value.snifferId to getCharacterBySnifferId(it.value.snifferId) }
            .map { it.first to getOrCreateGameInfo(it.second) }
            .forEach { it.second.snifferId = it.first }
    }

    private fun getOrCreateGameInfo(character: DofusCharacter): GameInfo {
        val pid = getCharacterPID(character)
            ?: error("There should be a PID associated to character : ${character.pseudo}")
        val gameInfo = gameInfoByCharacter.computeIfAbsent(character) { GameInfo(character) }
        gameInfo.pid = pid
        return gameInfo
    }

    private fun allSnifferRunning(): Boolean {
        return snifferByPid.values.firstOrNull { !it.isSnifferRunning() } == null
    }

    private fun removeDeadSniffers(deadEntries: List<MutableMap.MutableEntry<Long, DofusMessageReceiver>>) {
        deadEntries.onEach { it.value.interrupt() }
            .forEach { snifferByPid.remove(it.key) }
    }

    private fun startNewSniffers(newConnections: List<DofusConnection>) {
        newConnections.forEach {
            val sniffer = DofusMessageReceiver(it.serverIp, it.serverPort, it.hostIp, it.hostPort)
            sniffer.start()
            snifferByPid[it.pid] = sniffer
        }
    }

    fun getGameInfoBySnifferId(snifferId: Long): GameInfo {
        val characterName = getCharacterBySnifferId(snifferId)
        return gameInfoByCharacter[characterName]
            ?: error("Game info should be initialized for character : $characterName")
    }

    fun getGamePidBySnifferId(snifferId: Long): Long {
        return snifferByPid.entries.firstOrNull { it.value.snifferId == snifferId }?.key
            ?: error("There should be a PID associated to sniffer : $snifferId")
    }

    private fun getCharacterNameFromFrame(pid: Long): String? {
        return FRAME_NAME_REGEX.matchEntire(JNAUtil.getFrameName(pid))?.destructured?.component1()
    }

    private fun getCharacterBySnifferId(snifferId: Long): DofusCharacter {
        val pid = getGamePidBySnifferId(snifferId)
        return connectionByCharacter.entries.firstOrNull { it.value.pid == pid }?.key
            ?: error("There should be a character for sniffer : $snifferId")
    }

    fun getGameInfoByPID(pid: Long): GameInfo {
        val sniffer = snifferByPid[pid] ?: error("Sniffer should be initialized for PID : $pid")
        return getGameInfoBySnifferId(sniffer.snifferId)
    }

    fun getCharacterPID(character: DofusCharacter): Long? {
        if (!connectionByCharacter.containsKey(character)) {
            updateNetwork()
        }
        return connectionByCharacter[character]?.pid
    }

    fun getAllPIDs(): List<Long> {
        return connectionByCharacter.values.map { it.pid }
    }

    fun setGameInfo(character: DofusCharacter, gameInfo: GameInfo) {
        gameInfoByCharacter[character] = gameInfo
    }

}