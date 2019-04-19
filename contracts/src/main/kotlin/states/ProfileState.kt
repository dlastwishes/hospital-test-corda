package states

import net.corda.core.contracts.BelongsToContract
import contracts.MedicalContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

@BelongsToContract(MedicalContract::class)

data class ProfileState ( val employeeNo : Int,
                          val fname : String ,
                          val lname : String ,
                          val age : Int,
                          val sex : String,
                          val ownerCompany : Party,
                          val hospital : Party,
                          override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {


    override val participants: List<AbstractParty> get() = listOf(ownerCompany, hospital)
}