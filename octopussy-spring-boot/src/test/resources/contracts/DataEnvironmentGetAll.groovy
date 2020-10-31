import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'must get all data environments'
    request {
        method GET()
        url '/dataEnvironments'
    }
    response {
        status OK()
        body([[
            envId: 'db1000',
            properties: [
                url: 'jdbc:h2:mem:db1000',
                login: 'serge'
            ]
        ], [
            envId: 'db3000',
            properties: [
                abc: 'xyz'
            ]
        ]])
    }
}
