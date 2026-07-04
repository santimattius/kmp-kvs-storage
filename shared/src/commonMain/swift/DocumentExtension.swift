//
// Created by Santiago Mattiauda on 14/10/25.
//

import Foundation

public extension Document {
    func get<T: Codable>() async throws -> T {
        let stringData = try await self.read()
        guard let data = stringData.data(using: .utf8) else {
            throw NSError(
                domain: "KvsStorage",
                code: -2,
                userInfo: [NSLocalizedDescriptionKey: "Failed to convert String to Data"]
            )
        }

        return try JSONDecoder().decode(T.self, from: data)
    }

    func put<T: Codable>(value: T) async throws {
        let encodedData = try JSONEncoder().encode(value)
        guard let string = String(data: encodedData, encoding: .utf8) else {
            throw NSError(
                domain: "KvsStorage",
                code: -3,
                userInfo: [NSLocalizedDescriptionKey: "Failed to convert Data to String"]
            )
        }
        try await write(value: string)
    }
}