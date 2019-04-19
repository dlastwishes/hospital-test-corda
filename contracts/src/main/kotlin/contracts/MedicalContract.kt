package contracts

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import states.ProfileState
import states.BasicMedicalRecordState
import java.security.PublicKey

class MedicalContract : Contract {

    companion object {
        @JvmStatic
        val medicalContractId = "contracts.MedicalContract"
    }

    interface Commands : CommandData
        class Register : TypeOnlyCommandData(), Commands
        class AddBasicRecord : TypeOnlyCommandData(), Commands


    override fun verify(tx: LedgerTransaction) {

        val medicalCommand = tx.commands.requireSingleCommand<Commands>()
        val setOfSigners = medicalCommand.signers.toSet()

        when (medicalCommand.value) {
            is Register -> RegisterProfile(tx, setOfSigners)
            is AddBasicRecord -> verifyAddBasicMedicalRecord(tx, setOfSigners)
            else -> throw IllegalArgumentException("Unrecognised command.")
        }
    }

    private fun RegisterProfile(tx: LedgerTransaction , signers: Set<PublicKey>) = requireThat {

            val out = tx.outputsOfType<ProfileState>().single()
            "Age must be more than 18 year old" using (out.age >= 18)
            "hospital and staff company cannot be the same." using (out.ownerCompany != out.hospital)

    }

    private fun verifyAddBasicMedicalRecord(tx: LedgerTransaction , signers: Set<PublicKey>)  = requireThat {

            val out = tx.outputsOfType<BasicMedicalRecordState>().single()
            "hospital and staff company cannot be the same." using (out.ownerCompany != out.hospital)

        }

    }
