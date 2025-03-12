cd build

echo ""
echo "===== CONFIGURE AND BUILD PROJECT ON TEST MODE ====="
echo ""

cmake -D IS_TEST_BUILD=ON .

cmake --build .

echo ""
echo "===== RUN PROJECT EXECUTE ====="

./rainbowraycamera