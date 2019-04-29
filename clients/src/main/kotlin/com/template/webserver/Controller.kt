package com.template.webserver

import Flows.NewBasicMedicalRecordFlow.NewBasicRecord
import Flows.enrollmentFlow.Register
import com.fasterxml.jackson.databind.util.JSONPObject
import jdk.nashorn.internal.parser.JSONParser
import net.corda.core.contracts.ContractState
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import states.BasicMedicalRecordState
import states.ProfileState
import java.io.File
import java.io.InputStream
import javax.servlet.http.HttpServletRequest

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/medical/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    private val myLegalName = rpc.proxy.nodeInfo().legalIdentities.first().name


    @GetMapping(value = [ "me" ])
    fun whoami() = mapOf("me" to myLegalName)

    @GetMapping(value = "getAllEnrollment", produces = ["text/plain"])
    private fun getAllEnrollment() = proxy.vaultQueryBy<ProfileState>().states.toString()

    @GetMapping(value = "getAllRecord", produces = ["text/plain"])
    private fun getAllBasicRecord() = proxy.vaultQueryBy<BasicMedicalRecordState>().states.toString()


    @PostMapping(value = "register" , headers = [ "Content-Type=application/x-www-form-urlencoded" ])
    fun register(request: HttpServletRequest): ResponseEntity<String> {

        val employeeNo = request.getParameter("employeeNo").toInt()
        val fname = request.getParameter("fname").toString()
        val lname = request.getParameter("lname").toString()
        val age = request.getParameter("age").toInt()
        val sex = request.getParameter("sex").toString()

        val hospital = request.getParameter("hospital")
        val hospitalParty = CordaX500Name.parse(hospital)
        val partyHospital = proxy.wellKnownPartyFromX500Name(hospitalParty) ?:
        return ResponseEntity.badRequest().body("Party named $hospital cannot be found.\n")

        return try {
            val signedTx = proxy.startTrackedFlow(::Register , employeeNo
                    , fname , lname , age , sex, partyHospital ).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Register successfully.")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @PostMapping(value = "newPatientRecord" , headers = [ "Content-Type=application/x-www-form-urlencoded" ])
    fun newPatientRecord(request: HttpServletRequest): ResponseEntity<String> {

        val employeeNo = request.getParameter("employeeNo").toInt()
        val fname = request.getParameter("fname").toString()
        val lname = request.getParameter("lname").toString()
        val age = request.getParameter("age").toInt()
        val sex = request.getParameter("sex").toString()

        val hospital = request.getParameter("hospital")
        val hospitalParty = CordaX500Name.parse(hospital)
        val partyHospital = proxy.wellKnownPartyFromX500Name(hospitalParty) ?:
        return ResponseEntity.badRequest().body("Party named $hospital cannot be found.\n")

        return try {
            val signedTx = proxy.startTrackedFlow(::Register , employeeNo
                    , fname , lname , age , sex, partyHospital ).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Register successfully.")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }


    @PostMapping(value = "addBasicRecord" , headers = [ "Content-Type=multipart/form-data" ])
    fun addBasicRecord(request: HttpServletRequest): ResponseEntity<String> {

        val employeeNo = request.getParameter("employeeNo").toInt()
        val systolic = request.getParameter("systolic").toInt()
        val diastolic = request.getParameter("diastolic").toInt()

        val attachPart = request.getPart("file")
        val fileContent = attachPart.inputStream
        val uploadHash = proxy.uploadAttachment(fileContent)

        val hospital = request.getParameter("hospital")
        val hospitalParty = CordaX500Name.parse(hospital)
        val partyHospital = proxy.wellKnownPartyFromX500Name(hospitalParty) ?:
        return ResponseEntity.badRequest().body("Party named $hospital cannot be found.\n")

        return try {

            val signedTx = proxy.startTrackedFlow(::NewBasicRecord , employeeNo ,systolic , diastolic, partyHospital, uploadHash).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Add Basic Record successfully and attachment hash is $uploadHash")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

}



