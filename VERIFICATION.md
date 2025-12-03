# Build Verification Checklist

This document outlines the verification steps that need to be completed once network access to Maven repositories is available.

## Prerequisites

- Network access to:
  - dl.google.com (Google Maven Repository)
  - repo1.maven.org (Maven Central)
  - plugins.gradle.org (Gradle Plugin Portal)
- Java 17 installed
- Android SDK (optional, for running on device/emulator)

## Build Verification

### 1. Dependency Resolution
```bash
# Clean any previous build artifacts
./gradlew clean

# Download all dependencies
./gradlew build --refresh-dependencies --no-daemon
```

**Expected Result**: All dependencies download successfully without errors.

**Common Issues**:
- If AGP 8.3.2 cannot be found, try AGP 8.3.1 or 8.2.2
- If Kotlin 1.9.23 has issues, try 1.9.22 or 1.9.24
- Check that KSP version matches Kotlin version pattern

### 2. Compilation
```bash
# Compile the project
./gradlew assembleDebug --no-daemon
```

**Expected Result**: Project compiles without errors.

**Common Issues**:
- Enum serialization errors: Check @SerialName annotations
- Type mismatch errors: Verify Double vs String changes in Item model
- Import errors: May need to add kotlinx.serialization.json imports

### 3. Unit Tests
```bash
# Run unit tests
./gradlew test --no-daemon
```

**Expected Result**: All tests pass.

**Tests to Verify**:
- Model serialization/deserialization
- Enum parsing (LocationType, UserRole, WarrantyType)
- Numeric value handling (purchasePrice, estimatedValue as Double)

### 4. Code Quality Checks
```bash
# Run lint
./gradlew lint --no-daemon

# Check for deprecations
./gradlew build --warning-mode all --no-daemon
```

**Expected Result**: No critical lint errors, only expected deprecation warnings.

## Model Verification

### Test JSON Parsing

Create test cases to verify the updated models can parse responses from both 4.x and 5.x backends:

#### Location Model Test
```kotlin
@Test
fun testLocationWithNewFields() {
    val json = """
    {
        "id": "123",
        "name": "Test Location",
        "location_type": "residential",
        "owner_info": {
            "owner_name": "John Doe"
        }
    }
    """
    val location = Json.decodeFromString<Location>(json)
    assertEquals(LocationType.RESIDENTIAL, location.locationType)
    assertNotNull(location.ownerInfo)
}

@Test
fun testLocationBackwardCompatibility() {
    val json = """
    {
        "id": "123",
        "name": "Test Location"
    }
    """
    val location = Json.decodeFromString<Location>(json)
    assertNull(location.locationType)
    assertNull(location.ownerInfo)
}
```

#### Item Model Test
```kotlin
@Test
fun testItemWithNumericValues() {
    val json = """
    {
        "id": "123",
        "name": "Test Item",
        "purchase_price": 99.99,
        "estimated_value": 120.50
    }
    """
    val item = Json.decodeFromString<Item>(json)
    assertEquals(99.99, item.purchasePrice, 0.01)
    assertEquals(120.50, item.estimatedValue, 0.01)
}

@Test
fun testItemWithWarranties() {
    val json = """
    {
        "id": "123",
        "name": "Test Item",
        "warranties": [
            {
                "type": "manufacturer",
                "duration_months": 12
            }
        ]
    }
    """
    val item = Json.decodeFromString<Item>(json)
    assertEquals(1, item.warranties.size)
    assertEquals(WarrantyType.MANUFACTURER, item.warranties[0].type)
}
```

#### User Model Test
```kotlin
@Test
fun testUserWithRole() {
    val json = """
    {
        "id": "123",
        "email": "test@example.com",
        "role": "admin"
    }
    """
    val user = Json.decodeFromString<User>(json)
    assertEquals(UserRole.ADMIN, user.role)
}
```

## Integration Testing

### API Endpoint Tests (if NesVentory backend is available)

1. **Test Login**:
   ```bash
   # Should return access token
   curl -X POST http://localhost:8000/api/token \
     -d "username=test@example.com&password=testpass"
   ```

2. **Test Get Locations**:
   ```bash
   # Should return locations with new fields (if 5.x backend)
   curl http://localhost:8000/api/locations/ \
     -H "Authorization: Bearer <token>"
   ```

3. **Test Get Items**:
   ```bash
   # Should return items with warranties, documents (if 5.x backend)
   curl http://localhost:8000/api/items/ \
     -H "Authorization: Bearer <token>"
   ```

## Manual Testing

### Test Scenarios

1. **Login Flow**:
   - [ ] App launches without crashes
   - [ ] Can enter server settings
   - [ ] Can login with valid credentials
   - [ ] Invalid credentials show appropriate error

2. **Location Viewing**:
   - [ ] Locations list loads
   - [ ] Location details display correctly
   - [ ] New fields (owner info, insurance) display if present
   - [ ] App doesn't crash if new fields are missing

3. **Item Viewing**:
   - [ ] Items list loads
   - [ ] Item details display correctly
   - [ ] Prices/values display as currency
   - [ ] Warranties display if present
   - [ ] App doesn't crash if new fields are missing

4. **Data Persistence**:
   - [ ] Login credentials are saved
   - [ ] Can logout and login again
   - [ ] Server settings persist

## Security Scan

```bash
# Run security checks (requires CodeQL or similar tool)
# This would normally be run by GitHub Actions
```

**Expected Result**: No high or critical security vulnerabilities in code changes.

## Performance Testing

1. **Cold Start Time**: Measure app launch time
2. **API Response Handling**: Verify large item/location lists load smoothly
3. **Memory Usage**: Check for memory leaks with large datasets

## Regression Testing

Ensure existing functionality still works:
- [ ] Login flow unchanged
- [ ] Item listing unchanged
- [ ] Location listing unchanged
- [ ] Navigation unchanged
- [ ] Settings persistence unchanged

## Compatibility Testing

Test against multiple backend versions:
- [ ] NesVentory 4.x backend (backward compatibility)
- [ ] NesVentory 5.0 backend (new features)

## Device Testing

Test on multiple Android versions:
- [ ] Android 8.0 (API 26) - Minimum supported
- [ ] Android 12 (API 31) - Common version
- [ ] Android 14 (API 34) - Recent version
- [ ] Android 15 (API 35+) - Target version

## Sign-off Checklist

- [ ] All dependencies resolve correctly
- [ ] Project compiles without errors
- [ ] All unit tests pass
- [ ] No critical lint errors
- [ ] Models parse JSON correctly
- [ ] Backward compatibility verified
- [ ] No security vulnerabilities
- [ ] Manual testing completed
- [ ] Performance acceptable
- [ ] No regressions found

## Issues Found

Document any issues discovered during verification:

### Issue Template
```
**Issue**: [Description]
**Severity**: [Critical/High/Medium/Low]
**Steps to Reproduce**: [Steps]
**Expected**: [Expected behavior]
**Actual**: [Actual behavior]
**Resolution**: [How it was fixed]
```

## Notes

- If any verification step fails, document the failure and fix before merging
- Update CHANGELOG.md with any additional changes made during verification
- Consider creating integration tests for new model fields
- Plan UI updates in a separate PR after models are verified

## Approved By

- [ ] Developer (code verification)
- [ ] Reviewer (code review)
- [ ] QA (testing completed)
- [ ] Product Owner (features approved)

---

**Last Updated**: 2024-12-03
**Status**: Pending Network Access
