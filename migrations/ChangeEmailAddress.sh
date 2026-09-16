#!/bin/bash

echo ""
echo "Applying migration ChangeEmailAddress"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /changeEmailAddress                        controllers.ChangeEmailAddressController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /changeEmailAddress                        controllers.ChangeEmailAddressController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeChangeEmailAddress                  controllers.ChangeEmailAddressController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeChangeEmailAddress                  controllers.ChangeEmailAddressController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "changeEmailAddress.title = changeEmailAddress" >> ../conf/messages.en
echo "changeEmailAddress.heading = changeEmailAddress" >> ../conf/messages.en
echo "changeEmailAddress.checkYourAnswersLabel = changeEmailAddress" >> ../conf/messages.en
echo "changeEmailAddress.error.required = Enter changeEmailAddress" >> ../conf/messages.en
echo "changeEmailAddress.error.length = ChangeEmailAddress must be 70 characters or less" >> ../conf/messages.en
echo "changeEmailAddress.change.hidden = ChangeEmailAddress" >> ../conf/messages.en

echo "Migration ChangeEmailAddress completed"
