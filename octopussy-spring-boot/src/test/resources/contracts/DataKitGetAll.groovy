import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'must get all data kits'
    request {
        method GET()
        url '/dataKits'
    }
    response {
        status OK()
        body([[
            kitId: 'db1000',
            properties: [
                url: 'jdbc:h2:mem:db1000',
                login: 'serge'
            ]
        ], [
            kitId: 'db3000',
            properties: [
                abc: 'xyz'
            ]
        ]])
    }
}
