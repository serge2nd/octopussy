package ru.serge2nd.octopussy

import groovy.transform.CompileStatic
import org.springframework.cloud.contract.spec.Contract

import static java.lang.Integer.MAX_VALUE
import static java.lang.String.format
import static java.util.Collections.unmodifiableMap
import static org.springframework.cloud.contract.spec.Contract.assertContract

@CompileStatic
class AppContractsTesting {
    public static final String ID = 'db1000', ID2 = 'db3000'

    public static final Double V1 = 3.14159d
    public static final Integer V2 = MAX_VALUE
    public static final String K1 = V2.toString(), K2 = format("%f", V1)

    public static final String Q = 'select smth from smwh'
    public static final Map<String, Object> PARAM = unmodifiableMap(
        (K1): V1,
        (K2): V2
    ) as Map
    public static final List RS = [5, 7].asUnmodifiable()

    static Contract contract(String name, String desc = '', @DelegatesTo(Contract) Closure c) {
        Contract contract = new Contract(name: name, description: desc)
        c.delegate = contract; c.call()
        assertContract(contract)
        return contract
    }
}
