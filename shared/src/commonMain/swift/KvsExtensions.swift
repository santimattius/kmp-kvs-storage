//
// Created by Santiago Mattiauda on 28/9/25.
//

import Foundation
import KvsStorage

public extension Kvs {
    public func getStringAsResult(key : String, defValue: String) async -> Result<String, Error> {
        do {
            return .success(try await getString(key: key, defValue: defValue))
        } catch {
            return .failure(error)
        }
    }

    public func getIntAsResult(key : String, defValue: Int32) async -> Result<Int32, Error> {
        do {
            return .success(try await getInt(key: key, defValue: defValue).int32Value)
        } catch {
            return .failure(error)
        }
    }

    public func getLongAsResult(key : String, defValue: Int64) async -> Result<Int64, Error> {
        do {
            return .success(try await getLong(key: key, defValue: defValue).int64Value)
        } catch {
            return .failure(error)
        }
    }

    public func getFloatAsResult(key : String, defValue: Float) async -> Result<Float, Error> {
        do {
            return .success(try await getFloat(key: key, defValue: defValue).floatValue)
        } catch {
            return .failure(error)
        }
    }

    public func getBooleanAsResult(key : String, defValue: Bool) async -> Result<Bool, Error> {
        do {
            return .success(try await getBoolean(key: key, defValue: defValue).boolValue)
        } catch {
            return .failure(error)
        }
    }
}

public extension KvsKvsEditor {

    public func apply() async -> Result<Bool, Error>{
        do {
            try await commit()
            return .success(true)
        } catch {
            return .failure(error)
        }
    }
}