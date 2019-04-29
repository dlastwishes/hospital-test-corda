package states

import net.corda.core.contracts.BelongsToContract
import contracts.MedicalContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

@BelongsToContract(MedicalContract::class)

data class PatientRecord ( val employeeNo : Int,
                          val symptom : String,
                          val ownerCompany : Party,
                          val hospital : Party,
                          val doctorId : Party?,
                          override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {


    override val participants: List<AbstractParty> get() = listOf(ownerCompany, hospital)
}
